export interface Court {
  id: number;
  name: string;
  sportType: string;
  location: string;
  available: boolean;
  description: string;
  pricePerHour: number;
}