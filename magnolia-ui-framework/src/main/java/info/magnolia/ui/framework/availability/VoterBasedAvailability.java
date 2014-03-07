///**
// * This file Copyright (c) 2013 Magnolia International
// * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
// *
// *
// * This file is dual-licensed under both the Magnolia
// * Network Agreement and the GNU General Public License.
// * You may elect to use one or the other of these licenses.
// *
// * This file is distributed in the hope that it will be
// * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
// * implied warranty of MERCHANTABILITY or FITNESS FOR A
// * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
// * Redistribution, except as permitted by whichever of the GPL
// * or MNA you select, is prohibited.
// *
// * 1. For the GPL license (GPL), you can redistribute and/or
// * modify this file under the terms of the GNU General
// * Public License, Version 3, as published by the Free Software
// * Foundation.  You should have received a copy of the GNU
// * General Public License, Version 3 along with this program;
// * if not, write to the Free Software Foundation, Inc., 51
// * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
// *
// * 2. For the Magnolia Network Agreement (MNA), this file
// * and the accompanying materials are made available under the
// * terms of the MNA which accompanies this distribution, and
// * is available at http://www.magnolia-cms.com/mna.html
// *
// * Any modifications to this file must keep this entire header
// * intact.
// *
// */
//package info.magnolia.ui.framework.availability;
//
//import info.magnolia.ui.api.availability.voters.AccessGrantedToActionVoter;
//import info.magnolia.ui.api.availability.voters.MultipleItemsAllowedRule;
//import info.magnolia.ui.api.availability.voters.JcrItemNodeTypeAllowedRule;
//import info.magnolia.ui.api.availability.voters.JcrNodesAllowedRule;
//import info.magnolia.ui.api.availability.voters.ActionAvailableForPropertiesVoter;
//import info.magnolia.ui.api.availability.voters.IsRootItemAllowedRule;
//import info.magnolia.ui.api.availability.voters.ActionAvailableForRuleVoter;
//import info.magnolia.ui.api.availability.voters.AlwaysTrueAvailabilityRule;
//import info.magnolia.voting.Voter;
//import info.magnolia.voting.Voting;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import com.vaadin.data.Item;
//
///**
// * Availability definition based on {@link Voter} objects.
// */
//public class VoterBasedAvailability {
//
//    private List<Voter> voters = new ArrayList<Voter>();
//
//    public VoterBasedAvailability() {
//        List<Voter> voters = new ArrayList<Voter>();
//        voters.add(new JcrNodesAllowedRule(true));
//        voters.add(new ActionAvailableForPropertiesVoter(false));
//        voters.add(new IsRootItemAllowedRule(false));
//        voters.add(new MultipleItemsAllowedRule(false));
//        voters.add(new ActionAvailableForRuleVoter(AlwaysTrueAvailabilityRule.class.getName()));
//        voters.add(new JcrItemNodeTypeAllowedRule(new HashMap<String, String>()));
//        voters.add(new AccessGrantedToActionVoter(new HashMap<String, Object>()));
//        setVoters(voters);
//    }
//
//    public List<Voter> getVoters() {
//        return voters;
//    }
//
//    public void setVoters(List<Voter> voters) {
//        this.voters = voters;
//    }
//
//    public void addVoter(Voter voter) {
//        this.voters.add(voter);
//    }
//
//    public boolean isAvailableForItems(List<Item> items) {
//        return isAvailableForItems(items.toArray(new Item[items.size()]));
//    }
//
//    public boolean isAvailableForItems(Item[] items) {
//        final Voter[] voters = this.voters.toArray(new Voter[this.voters.size()]);
//        return Voting.AND.vote(voters, items) > 0;
//    }
//}
