package com.osm.blockchain.transacationservice.transacionservice.controllers;

import com.osm.blockchain.transacationservice.transacionservice.models.BlockchainTransaction;
import com.osm.blockchain.transacationservice.transacionservice.services.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class BlockChainController {

    private final BlockchainService blockchainService;

    @Autowired
    public BlockChainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @PostMapping("/transaction")
    public BlockchainTransaction execute(@RequestBody BlockchainTransaction transaction) throws IOException {
        return blockchainService.process(transaction);
    }
}
