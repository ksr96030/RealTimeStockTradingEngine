# RealTimeStockTradingEngine

Implement a real-time Stock trading engine for matching Stock Buys with Stock Sells.
1. Write an ‘addOrder’ function that will have the following parameters:
      ‘Order Type’ (Buy or Sell), ‘Ticker Symbol’, ‘Quantity’, ‘Price’
      Support 1,024 tickers (stocks) being traded.
      Write a wrapper to have this ‘addOrder’ function randomly execute with different parameter values to simulate active stock transactions.

2. Write a ‘matchOrder’ function, that will match Buy & Sell orders with the following criteria:
      Buy price for a particular ticker is greater than or equal to lowest Sell price available then.
      Write your code to handle race conditions when multiple threads modify the Stock order book, as run in real-life, by multiple stockbrokers. Also, use lock-free data structures.
      Do not use any dictionaries, maps or equivalent data structures. Essentially there should be no ‘import’-s nor ‘include’-s nor similar construct relevant to the programming language you are using that provides you dictionary, map or equivalent data structure capability. In essence, you are writing the entire code. Standard language-specific non data structure related items are ok, but try to avoid as best as you can.
      Write your ‘matchOrder’ function with a time-complexity of O(n), where 'n' is the number of orders in the Stock order book.
