import axios from 'axios';

const API_URL = 'https://iamaurimas.xyz/api/minter/';

class refundsService {


    startRefunds(data){
        return axios.post(API_URL + 'startrefunds', data)
    }
    stopRefunds(data){
        return axios.post(API_URL + 'stoprefunds', data)
    }
}

export default new refundsService()
