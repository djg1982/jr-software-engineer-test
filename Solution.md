# Approach to the Solution

## Entity Definitions

- **Order**: Represents an order with fields for `id`, `items`, and `status`. The `id` is a unique identifier, `items` is a list of `OrderItem` objects, and `status` indicates the current state of the order (e.g., PENDING, SUCCESS, CANCELLED).
- **OrderItem**: Represents an item within an order with fields for `id`, `quantity`, `order`, and `bookId`. The `id` is a unique identifier, `quantity` is the number of items ordered, `order` is a reference to the `Order` object, and `bookId` is the identifier of the book.

## Service Layer

- **OrderService**: Provides methods to create orders, retrieve orders, and update order status. It interacts with the `OrderRepository` and `BookStockService` to manage orders and update book stock.
  - **createOrder**: Validates stock availability for each `OrderItem`, assigns a unique `id` to the `Order`, sets the status to SUCCESS, associates the `Order` with its `OrderItem` objects, updates the stock, and saves the order.
  - **getOrders**: Retrieves all orders from the repository.
  - **updateOrderStatus**: Updates the status of an existing order.

## Repository Layer

- **OrderRepository**: Manages CRUD operations for `Order` entities.
- **BookStockService**: Manages book stock, including methods to get book stock and decrease stock.

## Testing

- **OrderServiceTest**: Contains unit tests for the `OrderService` methods using Mockito to mock dependencies.
  - **testCreateOrderSuccess**: Verifies that an order is created successfully when there is sufficient stock.
  - **testCreateOrderInsufficientStock**: Verifies that an exception is thrown when there is insufficient stock.
  - **testCreateOrderWithExistingInventory**: Verifies that an order is created successfully with existing inventory.
  - **testCreateOrderWithInventedBook**: Verifies that an exception is thrown when the book does not exist in the stock.

## Error Handling

- Ensures that appropriate exceptions are thrown and handled when there is insufficient stock or when an order is not found.

## Database Schema

- Defines the necessary tables (`book_order` and `order_item`) with appropriate relationships and constraints to support the application.

By following this approach, the solution ensures that orders are managed efficiently, stock availability is validated, and appropriate error handling is in place. The unit tests verify the functionality of the `OrderService` methods, ensuring that the application behaves as expected.