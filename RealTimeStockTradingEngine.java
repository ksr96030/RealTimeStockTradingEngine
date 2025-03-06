//Santhosh Reddy Komatireddy
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

class Order {
    enum Type { BUY, SELL }
    Type orderType;
    String tickerSymbol;
    int quantity;
    int price;
    Order nextOrder;

    public Order(Type orderType, String tickerSymbol, int quantity, int price) {
        this.orderType = orderType;
        this.tickerSymbol = tickerSymbol;
        this.quantity = quantity;
        this.price = price;
        this.nextOrder = null;
    }

    @Override
    public String toString() {
        return orderType + " " + quantity + " " + tickerSymbol + " @ $" + price;
    }
}

class StockOrderBook {
    private static final int MAX_TICKERS = 1024;
    @SuppressWarnings("unchecked")
    private final AtomicReference<Order>[] buyOrders = new AtomicReference[MAX_TICKERS];
    @SuppressWarnings("unchecked")
    private final AtomicReference<Order>[] sellOrders = new AtomicReference[MAX_TICKERS];

    public StockOrderBook() {
        for (int i = 0; i < MAX_TICKERS; i++) {
            buyOrders[i] = new AtomicReference<>(null);
            sellOrders[i] = new AtomicReference<>(null);
        }
    }

    private int getTickerIndex(String tickerSymbol) {
        return Math.abs(tickerSymbol.hashCode()) % MAX_TICKERS;
    }

    public void addOrder(Order.Type orderType, String tickerSymbol, int quantity, int price) {
        int index = getTickerIndex(tickerSymbol);
        Order newOrder = new Order(orderType, tickerSymbol, quantity, price);

        if (orderType == Order.Type.BUY) {
            insertOrder(buyOrders[index], newOrder, true);
        } else {
            insertOrder(sellOrders[index], newOrder, false);
        }
        
        matchOrder(index);
    }

    private void insertOrder(AtomicReference<Order> head, Order newOrder, boolean isBuyOrder) {
        while (true) {
            Order current = head.get();
            if (current == null || (isBuyOrder && newOrder.price >= current.price) || (!isBuyOrder && newOrder.price <= current.price)) {
                newOrder.nextOrder = current;
                if (head.compareAndSet(current, newOrder)) return;
            } else {
                Order prev = null, temp = current;
                while (temp != null && ((isBuyOrder && newOrder.price < temp.price) || (!isBuyOrder && newOrder.price > temp.price))) {
                    prev = temp;
                    temp = temp.nextOrder;
                }
                newOrder.nextOrder = temp;
                if (prev != null) {
                    prev.nextOrder = newOrder;
                    return;
                }
            }
        }
    }

    private void matchOrder(int index) {
        while (true) {
            Order buyOrder = buyOrders[index].get(), sellOrder = sellOrders[index].get();
            if (buyOrder == null || sellOrder == null || buyOrder.price < sellOrder.price) break;

            int matchedQuantity = Math.min(buyOrder.quantity, sellOrder.quantity);
            if (matchedQuantity == 0) break;

            buyOrder.quantity -= matchedQuantity;
            sellOrder.quantity -= matchedQuantity;

            System.out.println("Matched: " + matchedQuantity + " shares of " + buyOrder.tickerSymbol + " at $" + sellOrder.price);

            if (buyOrder.quantity == 0) buyOrders[index].compareAndSet(buyOrder, buyOrder.nextOrder);
            if (sellOrder.quantity == 0) sellOrders[index].compareAndSet(sellOrder, sellOrder.nextOrder);
        }
    }
}

class OrderSimulator implements Runnable {
    private static final String[] TICKERS = {"AAPL", "MSFT", "AMZN", "GOOGL", "TSLA"};
    private final StockOrderBook orderBook;
    private final int totalOrders;

    public OrderSimulator(StockOrderBook orderBook, int totalOrders) {
        this.orderBook = orderBook;
        this.totalOrders = totalOrders;
    }

    @Override
    public void run() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < totalOrders; i++) {
            Order.Type orderType = random.nextBoolean() ? Order.Type.BUY : Order.Type.SELL;
            String tickerSymbol = TICKERS[random.nextInt(TICKERS.length)];
            int quantity = random.nextInt(1, 101);
            int price = random.nextInt(50, 500);

            orderBook.addOrder(orderType, tickerSymbol, quantity, price);
            try {
                Thread.sleep(random.nextInt(5, 20)); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

public class RealTimeStockTradingEngine {
    public static void main(String[] args) {
        StockOrderBook orderBook = new StockOrderBook();
        int numberOfThreads = 5;
        int ordersPerThread = 10;

        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(new OrderSimulator(orderBook, ordersPerThread));
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Stock trading simulation completed.");
    }
}
