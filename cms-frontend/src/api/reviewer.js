import request from '@/utils/request'

// 获取待处理的审稿邀请列表
export function getPendingInvitations() {
    return request({
        url: '/api/reviewer/pending-invitations',
        method: 'get'
    })
}

// 接受/拒绝审稿邀请
export function respondToInvitation(data) {
    return request({
        url: '/api/reviewer/invitation/respond',
        method: 'post',
        data
    })
}

// 获取邀请的稿件详情
export function getManuscriptForReview(reviewId) {
    return request({
        url: `/api/reviewer/invitation/${reviewId}/manuscript`,
        method: 'get'
    })
}

// 下载/预览匿名稿件
export function downloadAnonymousManuscript(reviewId) {
    return request({
        url: `/api/reviewer/manuscript/download/${reviewId}`,
        method: 'get',
        responseType: 'blob'
    })
}

// 提交审稿意见
export function submitReview(data) {
    return request({
        url: '/api/reviewer/submit-review',
        method: 'post',
        data
    })
}

// [新增] 获取我的审稿任务列表（已接受/已完成）
export function getMyReviews() {
    return request({
        url: '/api/reviewer/my-reviews',
        method: 'get'
    })
}