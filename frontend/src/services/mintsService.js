import apiClient from './apiClient';

class mintsService {

    getMints(){
        return apiClient.get('getmints');
    }

    startMint(){
        return apiClient.post('startmint');
    }

    stopMint(){
        return apiClient.post('stopmint');
    }
}

export default new mintsService()
