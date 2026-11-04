package com.zhituxing.common;

import lombok.Getter;

import java.util.List;

/**
 * 分页响应结构
 */
@Getter
public class PageResult<T> {

    private final List<T> list;
    private final long total;
    private final int page;
    private final int pageSize;

    public PageResult(List<T> list, long total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }
}
