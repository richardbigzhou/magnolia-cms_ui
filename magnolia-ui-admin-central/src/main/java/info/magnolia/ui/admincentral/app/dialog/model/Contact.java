/**
 * This file Copyright (c) 2010-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.admincentral.app.dialog.model;

/**
 * Contact.
 *
 * @author ejervidalo
 */
public class Contact {

    public Contact(String city, String email, String familyName,
            String givenName, String name, String officeFax,
            String officePhone, String organizationName, String streetAddress,
            String zip, String web) {
        super();

        this.city = city;
        this.email = email;
        this.familyName = familyName;
        this.givenName = givenName;
        this.name = name;
        this.officeFax = officeFax;
        this.officePhone = officePhone;
        this.organizationName = organizationName;
        this.streetAddress = streetAddress;
        this.zip = zip;
        this.web = web;
    }

    /**
     *
     */
    public Contact() {
        // TODO Auto-generated constructor stub
    }

    private String city;
    private String email;
    private String familyName;
    private String givenName;
    private String name;
    private String officeFax;
    private String officePhone;
    private String organizationName;
    private String streetAddress;
    private String zip;
    private String web;

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getFamilyName() {
        return familyName;
    }
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    public String getGivenName() {
        return givenName;
    }
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getOfficeFax() {
        return officeFax;
    }
    public void setOfficeFax(String officeFax) {
        this.officeFax = officeFax;
    }
    public String getOfficePhone() {
        return officePhone;
    }
    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }
    public String getOrganizationName() {
        return organizationName;
    }
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    public String getStreetAddress() {
        return streetAddress;
    }
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }
    public String getZip() {
        return zip;
    }
    public void setZip(String zip) {
        this.zip = zip;
    }
    public String getWeb() {
        return web;
    }
    public void setWeb(String web) {
        this.web = web;
    }
}
