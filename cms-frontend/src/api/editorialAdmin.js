// src/api/editorialAdmin.js
import request from '@/utils/request'

// 形式审查相关接口
export function getTechCheckManuscripts() {
    return request({
        url: '/api/editorial-admin/manuscripts/tech-check',
        method: 'get'
    })
}

export function techCheck(data) {
    return request({
        url: '/api/editorial-admin/tech-check',
        method: 'post',
        data
    })
}

export function unsubmitManuscript(data) {
    return request({
        url: '/api/editorial-admin/unsubmit',
        method: 'post',
        data
    })
}

export function getTechCheckAnalysis(manuscriptId) {
    return request({
        url: '/api/editorial-admin/tech-check/analysis',
        method: 'get',
        params: { manuscriptId }
    })
}

// 新闻管理相关接口
export function getNewsList(params) {
    return request({
        url: '/api/news/list',
        method: 'get',
        params
    })
}

export function getNewsDetail(newsId) {
    return request({
        url: `/api/news/${newsId}`,
        method: 'get'
    })
}

export function addNews(data) {
    return request({
        url: '/api/news/add',
        method: 'post',
        data
    })
}

export function updateNews(data) {
    return request({
        url: '/api/news/update',
        method: 'put',
        data
    })
}

export function deleteNews(newsId) {
    return request({
        url: `/api/news/delete/${newsId}`,
        method: 'delete'
    })
}

export function uploadNewsFile(newsId, formData) {
    return request({
        url: `/api/news/upload-file/${newsId}`,
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

export function getNewsFiles(newsId) {
    return request({
        url: `/api/news/files/${newsId}`,
        method: 'get'
    })
}

export function deleteNewsFile(fileId) {
    return request({
        url: `/api/news/files/delete/${fileId}`,
        method: 'delete'
    })
}

export function downloadNewsFile(fileId) {
    return request({
        url: `/api/news/files/download/${fileId}`,
        method: 'get',
        responseType: 'blob'
    })
}