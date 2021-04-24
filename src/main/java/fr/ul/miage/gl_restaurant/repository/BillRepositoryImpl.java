package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Bill;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class BillRepositoryImpl extends Repository<Bill, Long> {

    private static final String FIND_ALL_SQL = "SELECT billId FROM Bills";
    private static final String FIND_BY_ID_SQL = "SELECT billId FROM Bills WHERE billId = ?";
    private static final String SAVE_SQL = "INSERT INTO Bills(billId) VALUES(?)";
    private static final String UPDATE_SQL = "UPDATE Bills SET billId = ? WHERE billId = ?";
    private static final String DELETE_SQL = "DELETE FROM Bills WHERE billId = ?";


    @Override
    public List<Bill> findAll() {
        return new ArrayList<>();
    }

    @Override
    public Optional<Bill> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Bill save(Bill object) {
        return null;
    }

    @Override
    public Bill update(Bill object) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}
