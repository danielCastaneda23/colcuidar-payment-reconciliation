package com.reconcilitation.reconcilitation.model;
/**
 * 
 * RejectedRow entity
 * @param rowNumber reject row number
 * @param reason reason of rejection.
 */
public record RejectedRow(long rowNumber, String reason) {
}
