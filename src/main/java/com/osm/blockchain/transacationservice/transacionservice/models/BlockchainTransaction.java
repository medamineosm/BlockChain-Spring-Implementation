package com.osm.blockchain.transacationservice.transacionservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockchainTransaction {

    private String id;
    private int fromId;
    private int toId;
    private long value;
    private boolean accepted;

    public BlockchainTransaction(int fromId, int toId, long value) {
        this.fromId = fromId;
        this.toId = toId;
        this.value = value;
    }

}
