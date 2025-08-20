import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'https://iamaurimas.xyz/api/minter/';

class configService {

    getAuthHeaders() {
        const token = localStorage.getItem('jwt');
        return token ? { Authorization: `Bearer ${token}` } : {};
    }

    getConfig(){
        return axios.get(API_URL + 'getconfig', { headers: this.getAuthHeaders() })
    }
    deletePolicy(){
        return axios.delete(API_URL + 'deletepolicy', { headers: this.getAuthHeaders() })
    }
    createPolicy(data){
        return axios.post(API_URL + 'createpolicy', data, { headers: this.getAuthHeaders() })
    }
    createMetadata(data){
        return axios.post(API_URL + 'createmetadata', data, { headers: this.getAuthHeaders() })
    }
    deleteMetadata(){
        return axios.delete(API_URL + 'deletemetadata', { headers: this.getAuthHeaders() })
    }
    setParameters(data){
        return axios.put(API_URL + 'setparameters', data, { headers: this.getAuthHeaders() })
    }
}

export default new configService()
