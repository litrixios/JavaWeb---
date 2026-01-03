import request from '@/utils/request'

// ================== 稿件全览 ==================
export function getAllManuscripts(status) {
    return request({
        url: '/api/eic/manuscript/list',
        method: 'get',
        params: { status }
    })
}

export function getManuscriptStatistics() {
    return request({
        url: '/api/eic/manuscript/statistics',
        method: 'get'
    })
}

// ================== 初审功能 ==================
export function deskReview(dto) {
    return request({
        url: '/api/eic/desk-review',
        method: 'post',
        data: dto
    })
}

// ================== 新增：批量初审函数 ==================
export function batchDeskReview(dtos) {
    return request({
        url: '/api/eic/desk-review/batch',
        method: 'post',
        data: dtos
    })
}

// ================== 撤销决定 ==================
export function rescindDecision(manuscriptId, newStatus, reason) {
    return request({
        url: '/api/eic/rescind-decision',
        method: 'post',
        params: { manuscriptId, newStatus, reason }
    })
}

// ================== 编辑管理 ==================
export function getEditorList() {
    return request({
        url: '/api/eic/editor/list',
        method: 'get'
    })
}

export function getEditorsByExpertise(expertise) {
    return request({
        url: '/api/eic/editor/expertise',
        method: 'get',
        params: { expertise }
    })
}

export function assignEditor(dto) {
    return request({
        url: '/api/eic/assign-editor',
        method: 'post',
        data: dto
    })
}

// ================== 终审决策 ==================
export function makeFinalDecision(dto) {
    return request({
        url: '/api/eic/final-decision',
        method: 'post',
        data: dto
    })
}

// ================== 特殊权限 ==================
export function withdrawManuscript(dto) {
    return request({
        url: '/api/eic/retract',
        method: 'post',
        data: dto
    })
}

// ================== 审稿人管理 ==================
export function getReviewerList() {
    return request({
        url: '/api/eic/reviewer/list',
        method: 'get'
    })
}

export function inviteReviewer(dto) {
    return request({
        url: '/api/eic/reviewer/invite',
        method: 'post',
        data: dto
    })
}

export function auditReviewer(userId, status) {
    return request({
        url: '/api/eic/reviewer/audit',
        method: 'post',
        params: { userId, status }
    })
}

export function removeReviewer(userId, reason) {
    return request({
        url: '/api/eic/reviewer/removes',
        method: 'post',
        params: { userId, reason }
    })
}