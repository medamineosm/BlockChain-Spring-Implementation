package com.osm.blockchain.transacationservice.transacionservice.services;

import com.osm.blockchain.transacationservice.transacionservice.models.BlockchainTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import rx.Subscription;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;

@Slf4j
@Service
public class BlockchainService {

    private final Web3j web3j;

    @Autowired
    public BlockchainService(Web3j web3j) {
        this.web3j = web3j;
    }

    @PostConstruct
    public void listen() {
        Subscription subscription = web3j.transactionObservable().subscribe(transaction -> {
            log.info("New transaction: id={}, block={}, from={}, to={}, value={}", transaction.getHash(), transaction.getBlockHash(), transaction.getFrom(), transaction.getTo(), transaction.getValue().intValue());
            try {
                EthCoinbase coinbase = web3j.ethCoinbase().send();
                EthGetTransactionCount transactionCount = web3j.ethGetTransactionCount(transaction.getFrom(), DefaultBlockParameterName.LATEST).send();
                log.info("Tx count: {}", transactionCount.getTransactionCount().intValue());
                if (transactionCount.getTransactionCount().intValue() % 10 == 0) {
                    EthGetTransactionCount tc = web3j.ethGetTransactionCount(coinbase.getAddress(), DefaultBlockParameterName.LATEST).send();
                    Transaction trx = Transaction.createEtherTransaction(coinbase.getAddress(), tc.getTransactionCount(), transaction.getValue(), BigInteger.valueOf(21_000), transaction.getFrom(), transaction.getValue());
                    web3j.ethSendTransaction(trx).send();
                }
            } catch (IOException e) {
                log.error("Error getting transactions", e);
            }
        });
        log.info("Subscribed");
    }

    public BlockchainTransaction process(BlockchainTransaction bcTransaction) throws IOException {
        EthAccounts ethAccounts = getAccounts();
        EthGetTransactionCount transactionCount = getEthTransactionCount(ethAccounts, bcTransaction);
        Transaction transaction = getTransacion(ethAccounts, transactionCount, bcTransaction);
        EthSendTransaction response = sendTransaction(transaction);

        if (response.getError() != null) {
            bcTransaction.setAccepted(false);
            return bcTransaction;
        }
        bcTransaction.setAccepted(true);
        String transactionHash = response.getTransactionHash();
        log.info("Transaction hash: {}", transactionHash);
        bcTransaction.setId(transactionHash);
        EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(transactionHash).send();
        if (receipt.getTransactionReceipt().isPresent()) {
            log.info("Transaction receipt: {}", receipt.getTransactionReceipt().get().getCumulativeGasUsed().intValue());
        }
        return bcTransaction;
    }

    private EthAccounts getAccounts() throws IOException {
        return web3j.ethAccounts().send();
    }

    private EthGetTransactionCount getEthTransactionCount(EthAccounts ethAccounts, BlockchainTransaction bcTransaction) throws IOException {
        return web3j.
                ethGetTransactionCount(
                        ethAccounts.getAccounts().get(bcTransaction.getFromId()),
                        DefaultBlockParameterName.LATEST).send();
    }

    private Transaction getTransacion(EthAccounts ethAccounts, EthGetTransactionCount ethGetTransactionCount, BlockchainTransaction bcTransaction){
        return Transaction.createEtherTransaction(
                ethAccounts.getAccounts().get(bcTransaction.getFromId()),
                ethGetTransactionCount.getTransactionCount(),
                BigInteger.valueOf(bcTransaction.getValue()),
                BigInteger.valueOf(21_000),
                ethAccounts.getAccounts().get(bcTransaction.getToId()),
                BigInteger.valueOf(bcTransaction.getValue()));
    }

    private EthSendTransaction sendTransaction(Transaction transaction) throws IOException {
        return web3j.ethSendTransaction(transaction).send();
    }
}