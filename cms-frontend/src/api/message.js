import request from '@/utils/request'

export function sendMessage(data) {
    return request({
        url: '/api/message/send',
        method: 'post',
        data
    })
}

export function getManuscriptHistory(manuscriptId) {
    return request({
        url: `/api/message/chat/${manuscriptId}`,
        method: 'get'
    })
}


export function getSystemNotifications() {
    return request({
        url: '/api/message/system-notifications',
        method: 'get'
    })
}

export function getChatSessions() {
    return request({
        url: '/api/message/sessions',
        method: 'get'
    })
}



export function markAllSystemAsRead() {
    return request({
        url: '/api/message/read-all-system',
        method: 'post'
    })
}

export function markTopicAsRead(topic) {
    return request({
        url: '/api/message/read-topic',
        method: 'post',
        params: { topic }
    })
}