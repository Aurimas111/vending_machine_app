import axios from 'axios';

const API_URL = 'https://iamaurimas.xyz/api/minter/';

class mintsService {

    getMints(data){
        return axios.post(API_URL + 'getmints', data)
    }
    startMint(data){
        return axios.post(API_URL + 'startmint', data)
    }
    stopMint(data){
        return axios.post(API_URL + 'stopmint', data)
    }
}

export default new mintsService()
