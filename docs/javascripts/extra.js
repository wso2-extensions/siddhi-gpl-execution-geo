/*
 * Copyright (C) 2017 WSO2 Inc. (http://wso2.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

var logoTitle = document.querySelector('.md-logo').title;
var extentionTitle = logoTitle.slice(7);
var header = document.querySelector('.md-header-nav__title');
var headerContent = document.querySelectorAll('.md-header-nav__topic')[1].textContent.trim();

header.innerHTML = '<span class="extention-title">' + extentionTitle + '</span>' + headerContent;
    