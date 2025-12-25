import request from '@/utils/request'

// 1. 发送消息
export function sendMessage(data) {
    return request({
        url: '/api/message/send',
        method: 'post',
        data
    })
}

// 2. 获取某篇稿件的历史消息记录
// 修正：后端 Controller 定义的路径是 /chat/{manuscriptId}
export function getManuscriptHistory(manuscriptId) {
    return request({
        url: `/api/message/chat/${manuscriptId}`,
        method: 'get'
    })
}

// 3. 获取系统通知 (新增)
// 对应后端 /api/message/system-notifications
export function getSystemNotifications() {
    return request({
        url: '/api/message/system-notifications',
        method: 'get'
    })
}

// 4. 获取会话列表 (新增)
// 注意：这里假设后端会新增一个 /sessions 接口返回 ChatSessionDTO 列表
// 如果你还没有写这个后端接口，请参考下文的后端修改建议
export function getChatSessions() {
    return request({
        url: '/api/message/sessions',
        method: 'get'
    })
}

// 5. (保留原有的) 获取我的消息列表
export function getMyMessages() {
    return request({
        url: '/api/message/list',
        method: 'get'
    })
}