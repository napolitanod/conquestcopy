package org.meb.conquest.deck;

import java.util.List;

import org.meb.conquest.db.model.loc.Card;

public interface DeckHelper {

	List<Card> filterValidDeckCards(List<Card> cards);
}