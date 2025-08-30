import apiClient from './apiClient';

class transactionsService {

    getTransactions(){
        return apiClient.get('transactions');
    }
}

export default new transactionsService()
