// src/utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
    baseURL: '', // 留空，让 vite 代理去处理
    timeout: 5000
})

service.interceptors.response.use(
    response => {
        const res = response.data
        // 假设后端返回格式是 { code: 200, data: ... }
        // 如果你们后端成功不返回 code=200，请自行调整这里的逻辑
        return res
    },
    error => {
        console.log('err' + error)
        ElMessage.error(error.message)
        return Promise.reject(error)
    }
)

export default service