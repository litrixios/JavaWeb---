import request from '@/utils/request'

// 1. 获取我的稿件列表 (对应 ManuscriptController 的 /my-list)
export function getMyManuscripts() {
    return request({
        url: '/api/manuscript/my-list',
        method: 'get'
    })
}

// 2. 投稿或保存草稿 (对应 ManuscriptController 的 /submit)
export function submitManuscript(data) {
    return request({
        url: '/api/manuscript/submit',
        method: 'post',
        data
    })
}

// 3. 发送消息 (对应 MessageController 的 /send)
export function sendMessage(data) {
    return request({
        url: '/api/message/send',
        method: 'post',
        data
    })
}

// 4. 上传文件 (如果你还没写这个后端接口，可以先留着，但前端调用会报404)
export function uploadFile(data) {
    return request({
        url: '/api/file/upload',
        method: 'post',
        data,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}
