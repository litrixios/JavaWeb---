import request from '@/utils/request'

// 1. 获取所有稿件全览 (对应 @GetMapping("/manuscript/list"))
export function getAllManuscripts(status) {
    return request({
        // 必须补上 /api，因为后端 RequestMapping 包含它，且你的 baseURL 为空
        url: '/api/eic/manuscript/list',
        method: 'get',
        params: { status }
    })
}

// 2. 指派副主编 (对应 @PostMapping("/assign-editor"))
// 参数 dto 包含: { manuscriptId: xx, editorId: xx }
export function assignEditor(dto) {
    return request({
        url: '/api/eic/assign-editor',
        method: 'post',
        data: dto // 后端是 @RequestBody，所以用 data
    })
}

// 3. 撤稿操作 (对应 @PostMapping("/retract"))
// 参数 dto 包含: { manuscriptId: xx, comments: '理由' }
export function withdrawManuscript(dto) {
    return request({
        url: '/api/eic/retract',
        method: 'post',
        data: dto // 后端是 @RequestBody，所以用 data
    })
}

// 4. 初审决策 (对应 @PostMapping("/desk-review"))
export function deskReview(dto) {
    return request({
        url: '/api/eic/desk-review',
        method: 'post',
        data: dto
    })
}

// 5. 移除审稿人 (对应 @PostMapping("/reviewer/remove"))
export function removeReviewer(userId, reason) {
    return request({
        url: '/api/eic/reviewer/remove',
        method: 'post',
        params: { userId, reason } // 后端是 @RequestParam，所以用 params
    })
}