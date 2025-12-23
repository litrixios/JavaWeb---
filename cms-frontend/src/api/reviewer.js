import request from '@/utils/request'

// 获取待处理的审稿邀请列表
export function getPendingInvitations() {
    return request({
        url: '/api/reviewer/pending-invitations',
        method: 'get'
    })
}

// 接受/拒绝审稿邀请
// data: { reviewId: number, isAccepted: boolean, reason?: string }
export function respondToInvitation(data) {
    return request({
        url: '/api/reviewer/invitation/respond',
        method: 'post',
        data
    })
}

// 获取邀请的稿件详情 (用于审稿页面)
export function getManuscriptForReview(reviewId) {
    return request({
        url: `/api/reviewer/invitation/${reviewId}/manuscript`,
        method: 'get'
    })
}

// 下载/预览匿名稿件
// 注意：设置 responseType 为 'blob' 以处理文件流
export function downloadAnonymousManuscript(reviewId) {
    return request({
        url: `/api/reviewer/manuscript/download/${reviewId}`,
        method: 'get',
        responseType: 'blob'
    })
}

// 提交审稿意见
// data: ReviewSubmitDTO
export function submitReview(data) {
    return request({
        url: '/api/reviewer/submit-review',
        method: 'post',
        data
    })
}