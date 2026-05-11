import Axios from 'axios';
import {Codes} from './Codes';
import axios from 'axios'

export function configLoginMoc(): boolean {
    return false;
}

let login =  '/login';

export async function confirm(context: any, message: string, title?: string) {
    try {
        let result = await context.$confirm(message, title, {type: "warning"});
        return true;
    } catch (e) {
        console.log(e);
    }
    return false;
};

export async function validateForm(thisForm: any) {
    let promise = new Promise((resolve, reject) => {
        thisForm.validate((valid: boolean) => {
            resolve(valid);
        });
    });
    return promise;
};

export async function axiosAction(method: string, url: string, data?: any): Promise<any> {
    let promise = new Promise((resolve, reject) => {
        Axios({
            method: method,
            url: url,
            data: data
        }).then(function (response) {
            // console.log(response.data);
            resolve(response.data);
        }).catch(function (error) {
            console.log(error);
            reject(error);
        })
    });
    return promise;
}

export async function doGet(context: any, url: string) {
    try {
        let result = await axiosAction('GET', url);
        return result;
    } catch (e) {
        var ret = {success: false, message: 'Failed to execute GET.'};
        return ret;
    }
};

export async function doGetImage(url: string) {
    try {
        const response = await axios({
            method: 'GET',
            url: url,
            responseType: 'blob'
        });
        return { success: true, data: response.data };
    } catch (e) {
        return {success: false, message: 'Failed to execute GET.', error: e}
    }
};

