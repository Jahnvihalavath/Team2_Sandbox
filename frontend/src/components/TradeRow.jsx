import React from 'react';

function TradeRowImpl({ trade, onClick }) {
  return (
    <>
      <span>{trade.tradeRef}</span>
      <span>{trade.symbol}</span>
      <span>{trade.qty}</span>
      <span>{trade.price}</span>
      <span>{trade.status}</span>
    </>
  );
}

function areEqual(prevProps, nextProps) {
  return (
    prevProps.trade.id === nextProps.trade.id &&
    prevProps.trade.status === nextProps.trade.status &&
    prevProps.trade.price === nextProps.trade.price &&
    prevProps.onClick === nextProps.onClick
  );
}

export default React.memo(TradeRowImpl, areEqual);