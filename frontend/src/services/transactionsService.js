import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'https://iamaurimas.xyz/api/minter/';

class transactionsService {

    getAuthHeaders() {
        const token = localStorage.getItem('jwt');
        return token ? { Authorization: `Bearer ${token}` } : {};
    }

    getTransactions(){
        return axios.get(API_URL + 'transactions', { headers: this.getAuthHeaders() })
    }
}

export default new transactionsService()
