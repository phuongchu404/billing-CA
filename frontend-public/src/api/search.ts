import request from '@/request/request';

export async function getSerialNumber(data: any) {
    return request({ url: '/api/cks', method: "get" , params: data });
}
