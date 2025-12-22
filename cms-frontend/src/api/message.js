// src/api/message.js
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
export function getManuscriptHistory(manuscriptId) {
    return request({
        url: `/api/message/history/${manuscriptId}`,
        method: 'get'
    })
}

// 3. 获取我的消息列表 (如果后面有消息中心的话会用到)
export function getMyMessages() {
    return request({
        url: '/api/message/list',
        method: 'get'
    })
}