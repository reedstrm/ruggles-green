#!/usr/bin/env python

from django.template import RequestContext
from django.shortcuts import render_to_response

class ForceWhitelistMiddleware(object):
  """Force user to be one of the whitelisted users."""

  def process_request(self, request):
    whitelist = [
      # Google Users
      'arjuns@google.com',
      'light@google.com',
      'tal@google.com',
      # CNX Users
      'baraniuk@gmail.com',
      'ed.woodward1@gmail.com',
      'jpslav@gmail.com',
      'kef.htx@gmail.com',
      'philschatz@gmail.com',
      'reedstrm@gmail.com',
    ]
    if request.user is None or request.user.email() not in whitelist:
      response = render_to_response('not_whitelisted.html', context_instance=RequestContext(request))
      response.status_code = 401
      return response
