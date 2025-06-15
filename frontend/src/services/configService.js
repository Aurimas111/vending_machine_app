import axios from 'axios';

const API_URL = 'https://iamaurimas.xyz/api/minter/';

class configService {

    getConfig(data){
        return axios.post(API_URL + 'getconfig', data)
    }
    setConfig(data){
        return axios.post(API_URL + 'setconfig', data)
    }
    deleteConfig(data){
        return axios.post(API_URL + 'deleteconfig', data)
    }
    deletePolicy(data){
        return axios.post(API_URL + 'deletepolicy', data)
    }
    createPolicy(data){
        return axios.post(API_URL + 'createpolicy', data)
    }
    createMetadata(data){
        return axios.post(API_URL + 'createmetadata', data)
    }
    deleteMetadata(data){
        return axios.post(API_URL + 'deletemetadata', data)
    }
    setParameters(data){
        return axios.post(API_URL + 'setparameters', data)
    }
}

export default new configService()
