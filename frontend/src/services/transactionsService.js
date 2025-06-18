import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'https://iamaurimas.xyz/api/minter/';

class transactionsService {

    getTransactions(data){
        return axios.post(API_URL + 'transactions', data)
    }
}

export default new transactionsService()
