package com.waylau.rednote.service;

import com.waylau.rednote.dto.UserActionEvent;

/**
 * RealTimeAnalysisService 实时分析服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/09/05
 **/
public interface RealTimeAnalysisService {
    /**
     * 实时统计热门笔记
     *
     * @return
     */
    void analyzeHotNotes(UserActionEvent userActionEvent);
}
