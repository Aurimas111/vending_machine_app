import axios from 'axios';

const API_URL = 'http://localhost:8080/api/refunds/';

class refundsService {


    startRefunds(data){
        return axios.post(API_URL + 'startrefunds', data)
    }
    stopRefunds(data){
        return axios.post(API_URL + 'stoprefunds', data)
    }
}

export default new refundsService()
