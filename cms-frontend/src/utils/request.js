// src/utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
    baseURL: '', // 留空，让 vite 代理去处理
    timeout: 5000
})

service.interceptors.request.use(
    config => {
        // 从本地存储获取 Token
        const token = localStorage.getItem('token')
        if (token) {
            // 将 Token 放入请求头，键名必须与后端 JwtInterceptor 里取的一致 ('Authorization')
            config.headers['Authorization'] = token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

service.interceptors.response.use(
    response => {
        const res = response.data
        return res
    },
    error => {
        console.log('err' + error)
        ElMessage.error(error.message)
        return Promise.reject(error)
    }
)

export default service