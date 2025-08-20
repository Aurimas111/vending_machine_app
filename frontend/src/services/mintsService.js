import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'https://iamaurimas.xyz/api/minter/';

class mintsService {

    getAuthHeaders() {
        const token = localStorage.getItem('jwt');
        return token ? { Authorization: `Bearer ${token}` } : {};
    }

    getMints(){
        return axios.get(API_URL + 'getmints', { headers: this.getAuthHeaders() })
    }
    startMint(){
        return axios.post(API_URL + 'startmint', {}, { headers: this.getAuthHeaders() })
    }
    stopMint(){
        return axios.post(API_URL + 'stopmint', {}, { headers: this.getAuthHeaders() })
    }
}

export default new mintsService()
