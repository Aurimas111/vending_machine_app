import axios from 'axios';

const API_URL = 'http://localhost:8080/api/minter/';

class transactionsService {

    getTransactions(data){
        return axios.post(API_URL + 'transactions', data)
    }
}

export default new transactionsService()
