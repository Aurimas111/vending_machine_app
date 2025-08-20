import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'https://iamaurimas.xyz/api/minter/';

class refundsService {

    getAuthHeaders() {
        const token = localStorage.getItem('jwt');
        return token ? { Authorization: `Bearer ${token}` } : {};
    }

    startRefunds(){
        return axios.post(API_URL + 'startrefunds', {}, { headers: this.getAuthHeaders() })
    }
    stopRefunds(){
        return axios.post(API_URL + 'stoprefunds', {}, { headers: this.getAuthHeaders() })
    }
}

export default new refundsService()
