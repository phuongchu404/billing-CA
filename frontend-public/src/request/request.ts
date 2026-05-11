import axios from 'axios';
import { ElMessage } from 'element-plus';
import store from '@/store/index'
import router from "@/router";

const apiBaseUrl = import.meta.env.VITE_API_URL;

console.log("Base URL in axios:", apiBaseUrl);
const http = axios.create({
    baseURL: apiBaseUrl,
    timeout: 1000 * 300,
    withCredentials: false,
    headers: {
        'Content-Type': 'application/json; charset=utf-8'
    }
})

/**
 *
 */
http.interceptors.request.use(config => {
    config.headers['token'] = sessionStorage.getItem('token');

    return config
}, error => {
    return Promise.reject(error)
})

/**
 *
 */
http.interceptors.response.use(response => {
    if (response.data.code && response.data.code != "0000") {
        if(response.data.code == "-1"){
            ElMessage.error("Log in failure")
            // store.commit('setUser',"")
            sessionStorage.removeItem("token")
            router.push("/")
        }else{
            ElMessage.error(response.data.message || "");
        }
    }
    return response
}, error => {
    if (error.response && error.response.status==401) {
        ElMessage.error("Log in failure");
        sessionStorage.removeItem("token")
        router.push("/")
    }else{
        // ElMessage.error("System error, contact administrator");
    }
    return Promise.reject(error)
})
export default http
