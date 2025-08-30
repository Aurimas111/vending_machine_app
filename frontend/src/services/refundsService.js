import apiClient from './apiClient';

class refundsService {


    startRefunds(){
        return apiClient.post('startrefunds');
    }

    stopRefunds(){
        return apiClient.post('stoprefunds');
    }
}

export default new refundsService()
