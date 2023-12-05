package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer  bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{
        int id=0;

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        // find the train from the db
        Optional<Train> optionalTrain=trainRepository.findById(bookTicketEntryDto.getTrainId());
        if(optionalTrain.isPresent()){
            Train train=optionalTrain.get();

            // Check if the train passes through the user specified route
            List<String> RouteList= Arrays.asList(train.getRoute().split(","));
            int StartPoint=RouteList.indexOf(bookTicketEntryDto.getFromStation().name());
            int EndPoint=RouteList.indexOf(bookTicketEntryDto.getToStation().name());

            if(StartPoint>=0  && StartPoint<EndPoint){
                // Train passes through the user specified points
                // check if seats are available during the entire journey from starting to ending point specified
                //
                int PassengersPresent=0;

                for(Ticket ticket: train.getBookedTickets()){
                    int ticketStartPoint=RouteList.indexOf(ticket.getFromStation().name());
                    int ticketEndPoint=RouteList.indexOf(ticket.getToStation().name());
                    if(ticketStartPoint<EndPoint && ticketEndPoint>StartPoint){
                        // This ticket's passengers will travel in train during our prefered journey stations
                        PassengersPresent+=ticket.getPassengersList().size();
                    }

                }
                int availableSeats=train.getNoOfSeats()-PassengersPresent;

                // Create a list of passengers by the ids provided by the user
                List<Passenger> passengerList=new ArrayList<>();

                for(int passengerId:bookTicketEntryDto.getPassengerIds()){
                    Optional<Passenger> optionalPassenger=passengerRepository.findById(passengerId);
                    if(optionalPassenger.isPresent()){
                        passengerList.add(optionalPassenger.get());
                    }
                }




                   // Now there are sufficient seats for booking the ticket preferred by user
                 Ticket ticket=new Ticket();
                 ticket.setFromStation(bookTicketEntryDto.getFromStation());
                 ticket.setToStation(bookTicketEntryDto.getToStation());
                 ticket.setTrain(train);;
                 ticket.setPassengersList(passengerList);
                int totalFare= 300 * passengerList.size()*(EndPoint-StartPoint);
                ticket.setTotalFare(totalFare);


                if(availableSeats<passengerList.size()){
                      return null;
                }

                train.getBookedTickets().add(ticket);
                trainRepository.save(train);
                Ticket ticket1=ticketRepository.save(ticket);
                id=ticket1.getTicketId();

                Optional<Passenger> optionalPassenger=passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
                if(optionalPassenger.isPresent()){
                    Passenger BookedPassenger=optionalPassenger.get();
                    BookedPassenger.getBookedTickets().add(ticket);

                }

            }else{
                throw new Exception("Invalid stations");
            }
        }

       return id;

    }
}
