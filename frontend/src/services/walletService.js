import * as Cardano from "@emurgo/cardano-serialization-lib-browser";

const API_URL = import.meta.env.VITE_API_URL || 'https://iamaurimas.xyz/api/minter/';

class WalletService {

  isWalletAvailable() {
    return window.cardano && window.cardano.eternl;
  }

  hexToBytes(hex) {
    return Uint8Array.from(hex.match(/.{1,2}/g).map(byte => parseInt(byte, 16)));
  }

  // Convert string to hex
  stringToHex(str) {
    return Array.from(str)
      .map(char => char.charCodeAt(0).toString(16).padStart(2, '0'))
      .join('');
  }

  // Get wallet address
  async getWalletAddress() {
    if (!this.isWalletAvailable()) {
      throw new Error("Eternl Wallet not found. Please install it.");
    }

    const api = await window.cardano.eternl.enable();
    const usedAddresses = await api.getUsedAddresses();

    if (usedAddresses.length === 0) {
      throw new Error("No used addresses found in wallet.");
    }

    const raw = usedAddresses[0];
    const address = Cardano.Address.from_bytes(this.hexToBytes(raw)).to_bech32();
    
    return { address, raw, api };
  }

  // Get nonce from backend
  async getNonce(address) {
    const response = await fetch(`${API_URL}/nonce?address=${address}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      }
    });

    if (!response.ok) {
      throw new Error('Failed to get nonce from backend');
    }

    const { nonce } = await response.json();
    
    return nonce;
  }

  // Sign data with wallet
  async signData(api, raw, nonce) {
    const hexNonce = this.stringToHex(nonce);
    const signed = await api.signData(raw, hexNonce);

    return signed;
  }

  // Authenticate with backend
  async authenticate(address, signedData) {
    const response = await fetch(`${API_URL}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        address: address,
        key: signedData.key,
        signature: signedData.signature,
      }),
    });

    if (!response.ok) {
      throw new Error('Failed to authenticate with backend');
    }

    const { token } = await response.json();
    return token;
  }

  // Complete wallet connection flow
  async connectWallet() {
    try {
      const { address, raw, api } = await this.getWalletAddress();

      const nonce = await this.getNonce(address);

      const signedData = await this.signData(api, raw, nonce);

      const token = await this.authenticate(address, signedData);

      localStorage.setItem("jwt", token);
      localStorage.setItem("wallet_connected", "true");
      localStorage.setItem("wallet_address", address);

      return { address, token };
    } catch (error) {
      console.error("Wallet connection failed", error);
      throw error;
    }
  }

  disconnectWallet() {
    localStorage.removeItem("jwt");
    localStorage.removeItem("wallet_connected");
    localStorage.removeItem("wallet_address");
  }

  isWalletConnected() {
    return localStorage.getItem("wallet_connected") === "true";
  }

  getStoredToken() {
    return localStorage.getItem("jwt");
  }

  getStoredAddress() {
    return localStorage.getItem("wallet_address");
  }

}

export default new WalletService();