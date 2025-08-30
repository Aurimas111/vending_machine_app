import apiClient from './apiClient';

class configService {
    getConfig(){
        return apiClient.get('getconfig');
    }
    deletePolicy(){
        return apiClient.delete('deletepolicy');
    }
    createPolicy(data){
        return apiClient.post('createpolicy', data);
    }
    createMetadata(data){
        return apiClient.post('createmetadata', data);
    }
    deleteMetadata(){
        return apiClient.delete('deletemetadata');
    }
    setParameters(data){
        return apiClient.put('setparameters', data);
    }
}

export default new configService();
