-- MySQL Init Script for Database: vending

CREATE DATABASE IF NOT EXISTS vending;
USE vending;

-- Table: metadata
CREATE TABLE metadata (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) DEFAULT NULL,
  file_url VARCHAR(255) DEFAULT NULL,
  image VARCHAR(255) DEFAULT NULL,
  ipfs_hash VARCHAR(255) DEFAULT NULL,
  is_minted TINYINT(1) DEFAULT NULL,
  attributes LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (JSON_VALID(attributes)),
  policy_id VARCHAR(200) NOT NULL,
  tx_hash VARCHAR(200) DEFAULT NULL,
  mint_blocktime INT DEFAULT NULL,
  receiver VARCHAR(200) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: policy
CREATE TABLE policy (
  id INT NOT NULL AUTO_INCREMENT,
  policyKeys VARCHAR(1000) NOT NULL,
  policyId VARCHAR(1000) NOT NULL,
  policyScript VARCHAR(1000) NOT NULL,
  name VARCHAR(100) NOT NULL,
  verificationKey VARCHAR(1000) NOT NULL,
  signingKey VARCHAR(1000) NOT NULL,
  ownerWallet VARCHAR(200) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: tx
CREATE TABLE tx (
  id INT NOT NULL AUTO_INCREMENT,
  txHash VARCHAR(200) NOT NULL,
  txIndex INT NOT NULL,
  blockHeight INT NOT NULL,
  blockTime INT NOT NULL,
  validAddress TINYINT(1) DEFAULT NULL,
  amountSent INT DEFAULT NULL,
  senderAddress VARCHAR(200) NOT NULL,
  refund INT DEFAULT NULL,
  refunded TINYINT(1) DEFAULT NULL,
  amountToMint INT NOT NULL DEFAULT 0,
  amountMinted INT NOT NULL DEFAULT 0,
  policyId VARCHAR(200) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (txHash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: userconfig
CREATE TABLE userconfig (
  id INT NOT NULL AUTO_INCREMENT,
  policy_id VARCHAR(200) DEFAULT NULL,
  policy_slot VARCHAR(200) DEFAULT NULL,
  collection_name VARCHAR(200) DEFAULT NULL,
  nft_price INT NOT NULL,
  collection_size INT DEFAULT NULL,
  nft_reserved_per_tx INT NOT NULL,
  nft_to_mint_per_tx INT NOT NULL,
  amount_of_nft_not_to_mint INT NOT NULL,
  refund_per_tx_limit INT NOT NULL,
  user_address VARCHAR(200) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (user_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
