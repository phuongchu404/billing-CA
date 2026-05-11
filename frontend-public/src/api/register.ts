import request from '@/request/request';

export function registerUser(data: any) {
    return request({ url: '/register', method: 'post', data });
}
