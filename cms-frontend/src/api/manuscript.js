import request from '@/utils/request'

// 投稿或保存草稿
export function submitManuscript(data) {
    return request({
        url: '/api/manuscript/submit',
        method: 'post',
        data
    })
}

// 获取我的稿件列表
export function getMyManuscripts(params) {
    return request({
        url: '/api/manuscript/my-manuscripts',
        method: 'get',
        params
    })
}

// 获取稿件详情及追踪信息
export function trackManuscript(manuscriptId) {
    return request({
        url: `/api/manuscript/track/${manuscriptId}`,
        method: 'get'
    })
}

// 提交修回
export function submitRevision(data) {
    return request({
        url: '/api/manuscript/submit-revision',
        method: 'post',
        data
    })
}