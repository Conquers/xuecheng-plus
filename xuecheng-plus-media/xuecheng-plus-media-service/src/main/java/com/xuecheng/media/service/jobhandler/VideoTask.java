package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/15 11:58
 */
@Slf4j
@Component
public class VideoTask {

    @Autowired
    MediaFileService mediaFileService;

    @Autowired
    MediaFileProcessService mediaFileProcessService;


    @Value("${videoprocess.ffmpegpath}")
    String ffmpegpath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        List<MediaProcess> mediaProcessList = null;
        int size = 0;
        try {
            //取出cpu核心数作为一次处理数据的条数
            int processors = Runtime.getRuntime().availableProcessors();
            //一次处理视频数量不要超过cpu核心数
            mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
            size = mediaProcessList.size();
            log.debug("取出待处理视频任务{}条", size);
            // 如果没有任务，那么返回
            if (mediaProcessList.isEmpty()) {
                log.debug("当前没有任务待处理");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //启动size个线程的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        //将处理任务加入线程池
        mediaProcessList.forEach(mediaProcess -> {
            threadPool.execute(() -> {
                // 执行
                try {
                    //任务id
                    Long taskId = mediaProcess.getId();
                    //抢占任务
                    boolean b = mediaFileProcessService.startTask(taskId);
                    // 没抢到返回，抢到了继续执行
                    if (!b) {
                        log.debug("抢占任务失败，任务id:{}", taskId);
                        return;
                    }
                    log.debug("开始执行任务:{}", mediaProcess);
                    //下边是处理逻辑
                    //桶
                    String bucket = mediaProcess.getBucket();
                    //存储路径
                    String filePath = mediaProcess.getFilePath();
                    //原始视频的md5值
                    String fileId = mediaProcess.getFileId();
                    //原始文件名称
                    String filename = mediaProcess.getFilename();
                    //将要处理的文件下载到服务器上
                    File originalFile = mediaFileService.downloadFileFromMinIO(mediaProcess.getBucket(), mediaProcess.getFilePath());
                    if (originalFile == null) {
                        log.debug("下载待处理文件失败,originalFile:{}", mediaProcess.getBucket().concat(mediaProcess.getFilePath()));
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "下载待处理文件失败");
                        return;
                    }
                    //处理结束的视频文件
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("mp4", ".mp4");
                    } catch (IOException e) {
                        log.error("创建mp4临时文件失败");
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "创建mp4临时文件失败");
                        return;
                    }
                    //视频处理结果
                    String result = "";
                    try {
                        //开始处理视频
                        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, originalFile.getAbsolutePath(), mp4File.getName(), mp4File.getAbsolutePath());
                        //开始视频转换，成功将返回success
                        result = videoUtil.generateMp4();
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("处理视频文件:{},出错:{}", mediaProcess.getFilePath(), e.getMessage());
                    }
                    if (!result.equals("success")) {
                        //记录错误信息
                        log.error("处理视频失败,视频地址:{},错误信息:{}", bucket + filePath, result);
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, result);
                        return;
                    }

                    //将mp4上传至minio
                    //mp4在minio的存储路径
                    String objectName = getFilePath(fileId, ".mp4");
                    //访问url
                    String url = "/" + bucket + "/" + objectName;
                    try {
                        mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), "video/mp4", bucket, objectName);
                        //将url存储至数据，并更新状态为成功，并将待处理视频记录删除存入历史
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "2", fileId, url, null);
                    } catch (Exception e) {
                        log.error("上传视频失败或入库失败,视频地址:{},错误信息:{}", bucket + objectName, e.getMessage());
                        //最终还是失败了
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "处理后视频上传或入库失败");
                    }
                } finally {
                    countDownLatch.countDown();
                }
            });
        });
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    @XxlJob("taskCompensationMechanismHandler")
    public void taskCompensationMechanismHandler() {
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList();
        for (MediaProcess mediaProcess : mediaProcessList) {
            if (mediaProcess.getFailCount() < 3) {
                // 获取创建后的时30分钟
                LocalDateTime deadline = mediaProcess.getCreateDate().plusMinutes(30);
                LocalDateTime now = LocalDateTime.now();
                if (now.isAfter(deadline)) {
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", mediaProcess.getFileId(), null, "超时");
                }
            }
        }
    }


    private String getFilePath(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    public static void main(String[] args) {
        int i = Runtime.getRuntime().availableProcessors();
        System.out.println("i = " + i);
    }
}
