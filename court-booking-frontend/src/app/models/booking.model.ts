export interface Booking {
  id: number;
  userId: number;
  courtId: number;
  courtName: string;
  bookingDate: string;
  startTime: string;
  endTime: string;
  status: string;
  notes: string;
  paymentId: number;
  amount: number;
}