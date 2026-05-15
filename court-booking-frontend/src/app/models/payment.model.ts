export interface Payment {
  id: number;
  bookingId: number;
  userId: number;
  amount: number;
  status: string;
  paymentMethod: string;
  transactionId: string;
}